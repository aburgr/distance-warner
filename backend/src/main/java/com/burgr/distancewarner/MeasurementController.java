package com.burgr.distancewarner;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeasurementController {
	
    Logger logger = LoggerFactory.getLogger(MeasurementController.class);
    
	@Autowired
	MeasurementRepository measurementRepository;
	
	@CrossOrigin
	@PostMapping("/api/measurement")
	public void postMeasurement(@Valid @RequestBody Measurement measurement) {
		logger.info("Saving measurement: " + measurement.toString());
		measurementRepository.save(measurement);
	}
}
