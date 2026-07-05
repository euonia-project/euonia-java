package com.euonia.sample.application.command;

import com.euonia.domain.command.CommandBase;

public class UserCreateCommand extends CommandBase {
    private String name;
    private String email;

    public UserCreateCommand() {
    }

    public UserCreateCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
