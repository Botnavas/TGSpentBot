package dev.botnavas.tgspentbot.models;

public class User {
    private long id;
    private String name;

    private UserStatus status;

    private String lastCommand;

    public enum UserStatus {
        UNREGISTERED(0),     // не зарегистрирован
        REGISTERED(1) ;     // базовая регистрация

        private final int code;

        UserStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static UserStatus fromCode(int code) {
            for (UserStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status code: " + code);
        }
    }

    public User() {
        id = 0;
        name = "";
        status = UserStatus.UNREGISTERED;
        lastCommand = "";
    }

    public User(long id, String name) {
        this.id = id;
        this.name = name;
        status = UserStatus.UNREGISTERED;
        lastCommand = "";
    }

    public User(long id, String name, int status) {
        this.id = id;
        this.name = name;
        this.status = UserStatus.fromCode(status);
        lastCommand = "";
    }

    public User(long id, String name, UserStatus status, String lastCommand) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.lastCommand = lastCommand;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }
}
