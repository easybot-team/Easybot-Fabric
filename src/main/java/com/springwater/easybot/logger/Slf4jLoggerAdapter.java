package com.springwater.easybot.logger;

import org.slf4j.Logger;
import com.springwater.easybot.bridge.logger.ILogger;

public class Slf4jLoggerAdapter implements ILogger {
    private final Logger logger;

    public Slf4jLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }
}