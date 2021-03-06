package common.messages;

import java.util.*;

public abstract class AbstractResponse {

    public int errorCode;

    public String errorMessage;

    public AbstractResponse() {
    }

    public AbstractResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
