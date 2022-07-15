package backend.drivor.base.domain.components;

import backend.drivor.base.domain.document.Account;
import backend.drivor.base.domain.document.ChatAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

@Component
public class SendMessageAsync {

    @Autowired
    private WebSocketConnect webSocketConnect;

    public void send(Account account, String message) throws Exception {

        ChatAccount chatAccount = webSocketConnect.getChatAccountInfo(account);

        // Connect to websocket server
        Session session = webSocketConnect.connect(chatAccount.getUsername(), chatAccount.getPassword());

        // Send message
        webSocketConnect.sendMessageAsync(message, session);

    }
}
