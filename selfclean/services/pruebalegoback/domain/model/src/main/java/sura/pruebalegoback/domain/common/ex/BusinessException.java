package sura.pruebalegoback.domain.common.ex;

import java.util.function.Supplier;

public class BusinessException extends ApplicationException {

    public enum Type {
        INVALID_TODO_INITIAL_DATA("Invalid TODO initial data"),
        TASK_NOT_FOUND("Indicated Task not found!"),
        USER_NOT_EXIST("Indicated User not exist!"),
        TASK_NOT_ASSIGNED("Task has not been assigned!"),
        TASK_ALREADY_ASSIGNED("Task already assigned!");

        private final String message;

        public String getMessage() {
            return message;
        }

        public BusinessException build() {
            return new BusinessException(this.message);
        }

        public Supplier<Throwable> defer() {
            return () -> new BusinessException(this.message);
        }

        Type(String message) {
            this.message = message;
        }

    }

    private final Type type;

    public BusinessException(String message){
        super(message, "BUSINESS_ERROR");
        this.type = null; // Se puede hacer null o crear un constructor adicional
    }

    @Override
    public String getCode(){
        if (type != null) {
            return type.name();
        }
        String parentCode = super.getCode();
        return parentCode != null ? parentCode : "BUSINESS_ERROR";
    }


}
