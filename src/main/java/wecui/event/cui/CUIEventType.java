package wecui.event.cui;

public enum CUIEventType {

    HANDSHAKE(CUIHandshakeEvent.class, "", 0),
    SELECTION(CUISelectionEvent.class, "s", 1),
    POINT(CUIPointEvent.class, "p", 5, 6),
    POINT2D(CUIPoint2DEvent.class, "p2", 4, 5),
    MINMAX(CUIMinMaxEvent.class, "mm", 2),
    VERSION(CUIVersionEvent.class, "v", 1),
    UPDATE(CUIUpdateEvent.class, "u", 1);
    
    private final Class<? extends CUIBaseEvent> eventClass;
    private final String key;
    private final int min;
    private final int max;
    
    private CUIEventType(Class<? extends CUIBaseEvent> eventClass, String key, int min, int max) {
        this.eventClass = eventClass;
        this.key = key;
        this.min = min;
        this.max = max;
    }

    private CUIEventType(Class<? extends CUIBaseEvent> eventClass, String key, int paramCount) {
        this.eventClass = eventClass;
        this.key = key;
        this.min = paramCount;
        this.max = paramCount;
    }

    public Class<? extends CUIBaseEvent> getEventClass() {
        return eventClass;
    }

    public String getKey() {
        return key;
    }

    public int getMaxParameters() {
        return max;
    }

    public int getMinParameters() {
        return min;
    }

    public static CUIEventType getTypeFromKey(String key) {
        for (CUIEventType value : CUIEventType.values()) {
            if (value.getKey().equals(key)) {
                return value;
            }
        }
        return null;
    }
}
