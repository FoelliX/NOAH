package de.foellix.aql.noah;

public class Triple {
	private String className;
	private String methodName;
	private String returnType;
	private Boolean source;

	private String jimpleStr;

	public Triple(String className, String methodName, String returnType) {
		this.className = className;
		this.methodName = methodName;
		this.returnType = returnType;
		this.source = null;
	}

	public boolean isValidExtractedString() {
		if (this.className.matches("[a-zA-Z0-9/.]+") && this.methodName.matches("[a-zA-Z0-9]+")
				&& this.returnType.matches("[a-zA-Z0-9/.()]+")) {
			if (this.className.contains("/") && this.returnType.contains(")L")) {
				return true;
			}
		}
		return false;
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getReturnType() {
		return this.returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public boolean isSource() {
		return this.source;
	}

	public void setSource(boolean source) {
		this.source = source;
	}

	public String getJimpleStr() {
		return this.jimpleStr;
	}

	public void setJimpleStr(String jimpleStr) {
		this.jimpleStr = jimpleStr;
	}

	@Override
	public String toString() {
		return this.className + ": " + this.returnType + " " + this.methodName
				+ (this.source != null ? " -> " + (this.source ? "SOURCE" : "SINK") : "");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Triple other = (Triple) obj;
		if (this.className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!this.className.equals(other.className)) {
			return false;
		}
		if (this.methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!this.methodName.equals(other.methodName)) {
			return false;
		}
		if (this.returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		} else if (!this.returnType.equals(other.returnType)) {
			return false;
		}
		return true;
	}
}
