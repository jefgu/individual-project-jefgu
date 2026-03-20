package handler;


public abstract class LogHandler implements Handler {
    private Handler next;
    @Override
    public void setNext(Handler next) {
        this.next = next;
    }
    @Override
    public abstract void handle(String line);
    public abstract void toJSON();
    protected boolean checkNext() {
        return next == null;
    }
    protected Handler getNext() {
        return next;
    }
}
