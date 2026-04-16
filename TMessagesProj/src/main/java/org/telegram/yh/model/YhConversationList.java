package org.telegram.yh.model;

import org.telegram.yh.YhApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YhConversationList {

    private final YhApiStatus status;
    private final List<YhConversation> conversations;
    private final long total;
    private final String requestId;

    public YhConversationList(YhApiStatus status, List<YhConversation> conversations, long total, String requestId) {
        this.status = status;
        this.conversations = conversations == null ? new ArrayList<YhConversation>() : conversations;
        this.total = total;
        this.requestId = requestId;
    }

    public YhApiStatus getStatus() {
        return status;
    }

    public List<YhConversation> getConversations() {
        return Collections.unmodifiableList(conversations);
    }

    public long getTotal() {
        return total;
    }

    public String getRequestId() {
        return requestId;
    }
}
