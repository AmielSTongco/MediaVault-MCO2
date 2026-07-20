package application.model;

public class Media {
	
	protected int mediaId;
	private Type type;
	private String title;
	private String creator;
	private int year;
	private Status status;
	private double userRating;
	private String review;
	private String info;
	private String yearString;
	
	// constructors
	public Media(int mediaId, Type type, String title, String creator, Status status, double userRating, String review, String info) {
		this.mediaId = mediaId;
		this.type = type;
		this.title = title;
		this.creator = creator;
		this.status = status;
		this.userRating = userRating;
		this.review = review;
		this.info = info;
	}
	
	// new
	public Media(String title, String creator, int year, Status status, double userRating, String review) {
		this.title = title;
		this.creator = creator;
		this.year = year;
		this.status = status;
		this.userRating = userRating;
		this.review = review;
	}
	
	// getters and setters
	public int getMediaId() {return mediaId;}
	public Type getMediaType() {return type;}
	public String getTitle() {return title;}
	public Status getStatus() {return status;}
	public String getCreator() {return creator;}
	public int getYear() {return year;}
	public double getUserRating() {return userRating;}
	public String getReview() {return review;}
	public String getUserRatingString() { 	
	    if(status == Status.COMPLETED) {
	    	return String.valueOf(userRating);
	    }
	    	
	    return "/-complete to rate-/";
	}
	public String getReviewedStatus() {
		if(status == Status.COMPLETED) {
			if(review.equals(""))
				return "no";
			else
				return "yes";
		}
	    	
	    return "/-complete to review-/";
	}
	public String getMediaInfo() {
		return info;
	}
	public String getYearString() {
		return yearString;
	}
	
	public void setMediaId(int mediaId) {this.mediaId = mediaId;}
	public void setMediaType(Type type) {this.type = type;}
	public void setTitle(String title) {this.title = title;}
	public void setStatus(Status status) {this.status = status;}
	public void setCreator(String creator) {this.creator = creator;}
	public void setUserRating(double userRating) {this.userRating = userRating;}
	public void setReview(String review) {this.review = review;}
	
    protected void setMediaInfo(String info) {
        this.info = info;
    }

    protected void setYearString(String yearString) {
        this.yearString = yearString;
    }
}
