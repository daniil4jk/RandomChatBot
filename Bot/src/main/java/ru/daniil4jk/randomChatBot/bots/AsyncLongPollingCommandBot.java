//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ru.daniil4jk.randomChatBot.bots;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.CommandRegistry;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * Used default TelegramLongPollingCommandBot class code, with one change:
 * the name of the method "onUpdateReceived" has been changed to "asyncUpdateHandler"
 */
public abstract class AsyncLongPollingCommandBot extends AsyncLongPollingBot implements CommandBot, ICommandRegistry {
    private final CommandRegistry commandRegistry;

    /** @deprecated */
    @Deprecated
    public AsyncLongPollingCommandBot() {
        this(new DefaultBotOptions());
    }

    /** @deprecated */
    @Deprecated
    public AsyncLongPollingCommandBot(DefaultBotOptions options) {
        this(options, true);
    }

    /** @deprecated */
    @Deprecated
    public AsyncLongPollingCommandBot(DefaultBotOptions options, boolean allowCommandsWithUsername) {
        super(options);
        this.commandRegistry = new CommandRegistry(allowCommandsWithUsername, this::getBotUsername);
    }

    public AsyncLongPollingCommandBot(String botToken) {
        this(new DefaultBotOptions(), botToken);
    }

    public AsyncLongPollingCommandBot(DefaultBotOptions options, String botToken) {
        this(options, true, botToken);
    }

    public AsyncLongPollingCommandBot(DefaultBotOptions options, boolean allowCommandsWithUsername, String botToken) {
        super(options, botToken);
        this.commandRegistry = new CommandRegistry(allowCommandsWithUsername, this::getBotUsername);
    }

    @Override
    public final void asyncUpdateHandler(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.isCommand() && !this.filter(message)) {
                if (!this.commandRegistry.executeCommand(this, message)) {
                    this.processInvalidCommandUpdate(update);
                }
                return;
            }
        }

        this.processNonCommandUpdate(update);
    }

    public final boolean register(IBotCommand botCommand) {
        return this.commandRegistry.register(botCommand);
    }

    public final Map<IBotCommand, Boolean> registerAll(IBotCommand... botCommands) {
        return this.commandRegistry.registerAll(botCommands);
    }

    public final boolean deregister(IBotCommand botCommand) {
        return this.commandRegistry.deregister(botCommand);
    }

    public final Map<IBotCommand, Boolean> deregisterAll(IBotCommand... botCommands) {
        return this.commandRegistry.deregisterAll(botCommands);
    }

    public final Collection<IBotCommand> getRegisteredCommands() {
        return this.commandRegistry.getRegisteredCommands();
    }

    public void registerDefaultAction(BiConsumer<AbsSender, Message> defaultConsumer) {
        this.commandRegistry.registerDefaultAction(defaultConsumer);
    }

    public final IBotCommand getRegisteredCommand(String commandIdentifier) {
        return this.commandRegistry.getRegisteredCommand(commandIdentifier);
    }

    public abstract String getBotUsername();
}
