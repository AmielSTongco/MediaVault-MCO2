package application.model;

public enum Type {
	SONG,
	GAME,
	SHOW;
	
	/**
	 * Converts the value to string for storing inside the database
	 * 
	 * @return lowercased string without underscores
	 */
	public String toDbString() {
        return this.name().toLowerCase(); 
    }
	
	/**
	 * Converts string to an uppercase snake case
	 * 
	 * @param value the string equivalent of status inside the database
	 * @return string in "screaming snake case"
	 */
    public static Type fromDbString(String value) {
        
        switch (value.toLowerCase()) {
            case "song":     
            	return SONG;
            case "game": 
            	return GAME;
            case "show":   
            	return SHOW;
            default: 
            	System.out.println("Unknown status: " + value);
            	return null;
        }
    }
}
