package application.model;

public enum Status {
	UNKNOWN,
	PLANNED,
	IN_PROGRESS,
	COMPLETED;
	
	/**
	 * Converts the value to string for storing inside the database
	 * 
	 * @return lowercased string without underscores
	 */
	public String toDbString() {
        return this.name().toLowerCase().replace("_", " "); 
    }
	
	/**
	 * Converts string to an uppercase snake case
	 * 
	 * @param value the string equivalent of status inside the database
	 * @return string in "screaming snake case"
	 */
    public static Status fromDbString(String value) {
        
        switch (value.toLowerCase()) {
            case "planned":     
            		return PLANNED;
            case "in progress": 
            	return IN_PROGRESS;
            case "completed":   
            		return COMPLETED;
            default: 
            		System.out.println("Unknown status: " + value);
            		return UNKNOWN;
        }
    }
}
