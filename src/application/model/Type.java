package application.model;

public enum Type {
	SONG,
	GAME,
	SHOW;
	
	/**
	 * Returns the pluralized title formatted in Title Case (e.g., "Songs", "Games", "Shows").
	 *
	 * @return the formatted media title
	 */
	public String getTitle() {
		String name = name();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase() + "s";
	}
	
	/**
	 * Returns the CSS style class name associated with this media type (e.g., "theme-songs").
	 *
	 * @return the CSS theme class string
	 */
	public String getStyleClass() {
		return "theme-" + name().toLowerCase() + "s";
	}
	
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
