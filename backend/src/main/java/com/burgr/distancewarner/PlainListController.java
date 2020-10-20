package com.burgr.distancewarner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlainListController {
	Logger logger = LoggerFactory.getLogger(PlainListController.class);

	@Autowired
	MeasurementRepository measurementRepository;
	
	@GetMapping("/list")
	public ResponseEntity<String> index() {
		List<Measurement> measurements = measurementRepository.findAll();
		logger.info(measurements.toString());
		
		StringBuilder sb = new StringBuilder();
		for(Measurement m : measurements)	{
			sb.append(m.toString());
			sb.append("<br />");
		}
		return ResponseEntity.ok(sb.toString());
	}
}
