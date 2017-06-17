package com.zik.faro.data;

public class MinUser {
	private String firstName;
    private String lastName;
    private String email;     
    private String expenseUserId;
    private String profileImageUrl;
    private String thumbProfileImageUrl;

    public MinUser() {          // For JAXB
    }

    public MinUser(final String firstName, final String lastName, final String email) {
        this(firstName, lastName, email, null);
    }

    public MinUser(final String firstName, final String lastName,  final String email, final String expenseUserId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.expenseUserId = expenseUserId;
    }
    
    public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getExpenseUserId() {
		return expenseUserId;
	}

	public void setExpenseUserId(String expenseUserId) {
		this.expenseUserId = expenseUserId;
	}

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getThumbProfileImageUrl() {
        return thumbProfileImageUrl;
    }

    public void setThumbProfileImageUrl(String thumbProfileImageUrl) {
        this.thumbProfileImageUrl = thumbProfileImageUrl;
    }

    public MinUser withFirstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    public MinUser withLastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    public MinUser withEmail(String email) {
        setEmail(email);
        return this;
    }

    public MinUser withExpenseUserId(String expenseUserId) {
        setExpenseUserId(expenseUserId);
        return this;
    }

    public MinUser withProfileImageUrl(String profileImageUrl) {
        setProfileImageUrl(profileImageUrl);
        return this;
    }

    public MinUser withThumbProfileImageUrl(String thumbProfileImageUrl) {
        setThumbProfileImageUrl(thumbProfileImageUrl);
        return this;
    }
}


