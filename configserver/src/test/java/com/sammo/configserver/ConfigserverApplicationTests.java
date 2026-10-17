package com.sammo.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "native"})
@SpringBootTest
class ConfigserverApplicationTests {

	@Test
	void contextLoads() {
	}

}
