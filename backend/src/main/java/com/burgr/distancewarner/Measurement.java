package com.burgr.distancewarner;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Positive;

@Entity
public class Measurement {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private Timestamp timestamp;

	@Column(nullable = false)
	@Positive
	private Double longitude;

	@Column(nullable = false)
	@Positive
	private Double latitude;

	@Column(nullable = false)
	@Positive
	private Double distance;

	@Column(nullable = false)
	@Positive
	private Double speed;

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	@Override
	public String toString() {
		return "Measurement [id=" + id + ", timestamp=" + timestamp + ", longitude=" + longitude + ", latitude="
				+ latitude + ", distance=" + distance + ", speed=" + speed + "]";
	}
}
