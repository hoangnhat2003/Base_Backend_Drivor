package backend.drivor.base.facade;

import backend.drivor.base.domain.utils.GsonSingleton;
import backend.drivor.base.domain.components.SmackClient;
import backend.drivor.base.domain.document.ChatAccount;
import backend.drivor.base.domain.exception.XMPPGenericException;
import backend.drivor.base.domain.message.AdminMessage;
import backend.drivor.base.domain.repository.ChatAccountRepository;
import backend.drivor.base.domain.utils.LoggerUtil;
import backend.drivor.base.domain.utils.ServiceExceptionUtils;
import lombok.RequiredArgsConstructor;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

import javax.websocket.Session;
import java.util.*;

@Component
@RequiredArgsConstructor
public class XMPPFacade {

    private static final String TAG = XMPPFacade.class.getSimpleName();

    private static final Map<Session, XMPPTCPConnection> CONNECTIONS = new HashMap<>();

    @Autowired
    private SmackClient smackClient;

    @Autowired
    private ChatAccountRepository chatAccountRepository;

    public void startSession(Session session, String username, String password) {

        Optional<ChatAccount> chatAccount = Optional.ofNullable(chatAccountRepository.findByUsername(username));

        if (!chatAccount.isPresent()) {
             throw ServiceExceptionUtils.accountNotFound();
        }

        Optional<XMPPTCPConnection> connection = smackClient.connect(username, password);

        if (!connection.isPresent()) {
            throw ServiceExceptionUtils.connectionFailed();
        }

        CONNECTIONS.put(session, connection.get());
        LoggerUtil.i(TAG, "Session was stored.");

        try {
            smackClient.login(connection.get());
        } catch (XMPPGenericException e) {
            handleXMPPGenericException(session, connection.get(), e);
            return;
        }

        smackClient.addIncomingMessageListener(connection.get(), session);

    }

    public void sendMessage(AdminMessage message, Session session) {
        XMPPTCPConnection connection = CONNECTIONS.get(session);

        if (connection == null) {
            return;
        }

        try {
            smackClient.sendMessage(connection,GsonSingleton.getInstance().toJson(message) , message.getTo());
        } catch (XMPPGenericException e) {
            handleXMPPGenericException(session, connection, e);
        }
    }

    public void disconnect(Session session) {

        XMPPTCPConnection connection = CONNECTIONS.get(session);

        if (connection == null) {
            return;
        }

        smackClient.disconnect(connection);
        CONNECTIONS.remove(session);
    }

    private void handleXMPPGenericException(Session session, XMPPTCPConnection connection, Exception e) {

        LoggerUtil.exception(TAG, e);
        LoggerUtil.e(TAG, "XMPP error. Disconnecting and removing session...");
        smackClient.disconnect(connection);
        CONNECTIONS.remove(session);
    }

}