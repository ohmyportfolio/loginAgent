package net.mycorp.jimin.base.common.services;

public enum Permit {
	INHERITED(0), NONE(1), READ(2), WRITE(3), DELETE(4);
	
    private final int value;
	
    private Permit(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
}
