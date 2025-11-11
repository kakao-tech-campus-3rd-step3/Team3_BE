package com.shootdoori.match.policy;

public class PasswordPolicy {
    private PasswordPolicy() {}

    public static final String REGEXP =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&~])[A-Za-z\\d@$!%*#?&~]{8,20}$";

    public static final String MESSAGE =
            "비밀번호는 8자 이상 20자 이하로 영문, 숫자, 특수문자(@,$,!,%,*,#,?,&,~)를 포함해야 합니다.";

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 20;
}
