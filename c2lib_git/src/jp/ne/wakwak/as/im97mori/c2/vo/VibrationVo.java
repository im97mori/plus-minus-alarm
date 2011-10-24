package jp.ne.wakwak.as.im97mori.c2.vo;

public class VibrationVo {
	private long id;
	private String name;
	private long[] pattern;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long[] getPattern() {
		return pattern;
	}

	public void setPattern(long[] pattern) {
		this.pattern = pattern;
	}
}