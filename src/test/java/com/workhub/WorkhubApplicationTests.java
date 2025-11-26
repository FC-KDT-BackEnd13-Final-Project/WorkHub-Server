package com.workhub;

import com.workhub.config.TestS3Config;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@EnableAutoConfiguration(exclude = {S3AutoConfiguration.class})
@Import(TestS3Config.class)
class WorkhubApplicationTests {

	@Test
	void contextLoads() {
	}

}
