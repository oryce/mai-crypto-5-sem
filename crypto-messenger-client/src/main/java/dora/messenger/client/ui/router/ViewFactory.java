package dora.messenger.client.ui.router;

import dora.messenger.client.store.chat.Chat;
import dora.messenger.client.ui.chat.ChatView;

public interface ViewFactory {

    ChatView chat(Chat chat);
}
