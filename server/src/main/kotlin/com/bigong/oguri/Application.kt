package com.bigong.oguri

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // 로컬 개발 환경에서 .env 파일을 읽어 시스템 속성으로 등록
            loadEnv()
            runApplication<Application>(*args)
        }

        /**
         * 프로젝트 루트의 .env 파일을 읽어 시스템 속성(System Property)으로 등록합니다.
         */
        private fun loadEnv() {
            val envFile = File(".env")
            if (envFile.exists()) {
                envFile.readLines().forEach { line ->
                    if (line.isNotBlank() && !line.startsWith("#")) {
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) {
                            val key = parts[0].trim()
                            val value = parts[1].trim()
                            if (System.getenv(key) == null && System.getProperty(key) == null) {
                                System.setProperty(key, value)
                            }
                        }
                    }
                }
            }
        }
    }
}

