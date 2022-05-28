package org.heckcorp.domination;

public class ShadowStatus {
    public ShadowStatus(boolean explored, boolean visible) {
        this.explored = explored;
        this.visible = visible;
    }
    
    // TODO: use generics to make this equals(ShadowStatus)?
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ShadowStatus
                && explored == ((ShadowStatus) obj).explored
                && visible == ((ShadowStatus) obj).visible;
    }
    
    /**
     * @uml.property  name="explored"
     */
    private final boolean explored;
    /**
     * @uml.property  name="visible"
     */
    private final boolean visible;
    
    public boolean isExplored() {
        return explored;
    }

    public boolean isVisible() {
        return visible;
    }
}