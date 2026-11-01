package br.edu.ifsp.kaluana.contact;

public enum ContactMethod {
    LIGACAO,
    MENSAGEM,
    WHATSAPP,
    EMAIL;

    public boolean isPhone() {
        return this != EMAIL;
    }
}
