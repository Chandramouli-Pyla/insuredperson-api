package org.example.insuredperson.DTO;


public class APIResponse<T> {
    private int status;
    private String message;
    private T data;

    public APIResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    public void setStatus(int status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
}
