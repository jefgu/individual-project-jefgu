package handler;

public interface Handler {
    void setNext(Handler next);
    void handle(String line);
}
