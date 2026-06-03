package com.mariasorganics.billing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
class MoBillingApplicationTests {

	@Autowired
	private com.mariasorganics.billing.service.EstimateService estimateService;

	@Test
	void contextLoads() {
	}

}
