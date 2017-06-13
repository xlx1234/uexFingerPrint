package com.test.vo;

import java.io.Serializable;


public class AuthenticateVO implements Serializable {
    private static final long serialVersionUID = -5388435368070864111L;
    private int maxTries = 3;

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }
}
