package br.edu.ifsp.kaluana.task;

/** RF17: o que fazer com uma tarefa pendente na revisão do fim do dia. */
public enum ReviewAction {
    /** A data passa a ser o dia seguinte ao revisado. */
    TOMORROW,
    /** Fica sem data (sai do plano dos próximos dias até ganhar uma). */
    NO_DATE,
    /** Foi feita e só faltou marcar. */
    DONE
}
