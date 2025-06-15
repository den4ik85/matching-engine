package com.interview.sample.service;

import com.interview.sample.controller.command.Command;

public interface CommandHandler<T extends Command> {
    void handle(T command);
}