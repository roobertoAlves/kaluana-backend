package br.edu.ifsp.kaluana.assistant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Adaptador para qualquer API compatível com "chat completions" da OpenAI:
 * Ollama local (gratuito, padrão), ou provedores com plano gratuito. Trocar de
 * provedor é só configuração (base-url, model, api-key).
 */
class OpenAiCompatiblePlanningAssistant implements PlanningAssistant {

    // As instruções limitam o modelo a organizar tarefas: nada de saúde,
    // diagnóstico ou conselho (RNF6, RNF7). A resposta ainda é validada no parser.
    private static final String ORDER_INSTRUCTIONS = """
            Você ajuda uma pessoa a decidir a ordem das tarefas do dia.
            Considere a duração e o nível de energia informado (1 = dia difícil, 5 = dia tranquilo):
            com energia baixa, prefira começar por tarefas curtas.
            Não dê conselhos de saúde, diagnósticos nem comentários; apenas ordene.
            Responda somente com JSON no formato {"ordem": [índices]} usando os índices recebidos.""";

    private static final String STEPS_INSTRUCTIONS = """
            Você divide uma tarefa em microetapas concretas e curtas (até 8), cada uma começando por um verbo.
            Não dê conselhos de saúde, diagnósticos nem comentários.
            Responda somente com JSON no formato {"etapas": ["...", "..."]}.""";

    private static final int MAX_TOKENS = 600;

    private final RestClient client;
    private final String model;
    private final String reasoningEffort;

    OpenAiCompatiblePlanningAssistant(AssistantProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (!properties.apiKey().isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey());
        }
        this.client = builder.build();
        this.model = properties.model();
        this.reasoningEffort = properties.reasoningEffort().isBlank() ? null : properties.reasoningEffort();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String modelName() {
        return model;
    }

    @Override
    public List<Integer> suggestOrder(List<TaskSummary> tasks, Integer energyLevel) {
        String taskList = IntStream.range(0, tasks.size())
                .mapToObj(i -> i + ": " + tasks.get(i).title() + " (" + tasks.get(i).minutes() + " min)")
                .collect(Collectors.joining("\n"));
        String energy = energyLevel != null ? energyLevel.toString() : "não informado";
        String content = complete(ORDER_INSTRUCTIONS, "Energia: " + energy + "\nTarefas:\n" + taskList);
        return AssistantResponseParser.parseOrder(content, tasks.size());
    }

    @Override
    public List<String> suggestSteps(String taskTitle) {
        return AssistantResponseParser.parseSteps(complete(STEPS_INSTRUCTIONS, "Tarefa: " + taskTitle));
    }

    private String complete(String instructions, String input) {
        ChatRequest request = new ChatRequest(model, List.of(
                new Message("system", instructions),
                new Message("user", input)), 0.2, MAX_TOKENS, reasoningEffort);
        try {
            ChatResponse response = client.post().uri("/chat/completions").body(request).retrieve().body(ChatResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AssistantUnavailableException("Resposta vazia do serviço de IA.");
            }
            return response.choices().get(0).message().content();
        } catch (RestClientException e) {
            throw new AssistantUnavailableException("Serviço de IA indisponível.", e);
        }
    }

    record Message(String role, String content) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ChatRequest(String model, List<Message> messages, double temperature,
                       @JsonProperty("max_tokens") int maxTokens,
                       @JsonProperty("reasoning_effort") String reasoningEffort) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(Message message) {
    }
}
