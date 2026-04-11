package com.bigong.oguri

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    // 로컬 개발 환경에서 .env 파일을 읽어 시스템 속성으로 등록
    loadEnv()
    runApplication<Application>(*args)
}

/**
 * 프로젝트 루트의 .env 파일을 읽어 시스템 속성(System Property)으로 등록합니다.
 * 이를 통해 로컬 개발 시 IntelliJ 설정 없이도 환경 변수를 편리하게 사용할 수 있습니다.
 * 배포 환경에서는 실제 시스템 환경 변수가 우선되도록 설계되었습니다.
 */
private fun loadEnv() {
    val envFile = File(".env")
    if (envFile.exists()) {
        envFile.readLines().forEach { line ->
            // 빈 줄이 아니고 주석(#)으로 시작하지 않는 줄만 처리
            if (line.isNotBlank() && !line.startsWith("#")) {
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    // 실제 시스템 환경 변수나 이미 설정된 속성이 없는 경우에만 주입 (중복 방지)
                    if (System.getenv(key) == null && System.getProperty(key) == null) {
                        System.setProperty(key, value)
                    }
                }
            }
        }
    }
}

