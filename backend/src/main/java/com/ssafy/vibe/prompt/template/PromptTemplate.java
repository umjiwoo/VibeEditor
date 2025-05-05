package com.ssafy.vibe.prompt.template;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class PromptTemplate {
	private String promptTemplate =
		"""
			# 기술 블로그 포스트 생성 요청
			
			당신은 주어진 정보를 바탕으로 명확하고 유익한 기술 블로그 포스트를 Notion에 업로드할 rich text 형식으로 작성하는 AI 어시스턴트입니다.
			
			## 제공 정보
			
			다음은 블로그 포스트 작성을 위해 제공되는 정보입니다. 각 항목을 주의 깊게 분석하여 포스트 내용에 반영해주세요.
			
			* **`postType`**: %s
			* **`content`**:
			    ```
			    %s
			    ```
			* **`userComment`**: %s
			* **`option`**: %s
			
			## 작성 가이드라인
			
			1.  **형식**: 최종 결과물은 반드시 **Markdown 형식**이어야 합니다.
			2.  **구조**: 다음과 같은 명확한 구조를 갖춰주세요.
			    * **제목**: `postType`과 `content`의 핵심 내용을 반영하는 매력적인 제목 (Markdown H1 또는 H2 사용)
			    * **서론**: 서론에서 인삿말은 제외하며, 포스트의 주제와 목적을 간략하게 소개합니다. `userComment`의 내용을 참고하여 독자의 흥미를 유발할 수 있습니다.
			    * **본론**:
			        * `description`을 바탕으로 `snapshot`의 내용을 상세하게 설명합니다.
			        * 코드는 Markdown 코드 블록(``` shift+enter code shift+enter ```)으로 감싸서 표현합니다.
			        * `postType`이 '트러블슈팅'인 경우, 문제 상황, 원인 분석, 해결 과정을 명확히 제시합니다.
			        * `postType`이 'CS 정리'인 경우, 개념을 이해하기 쉽게 설명하고 `content`를 예시로 활용합니다.
			    * **결론**: 포스팅의 내용을 요약하며, 포스팅 내용의 핵심을 해시태그 배열로 제공합니다. 마무리 인사는 생략합니다.
			3.  **내용**:
			    * 제공된 모든 정보 (`postType`, `content`, `userComment`)를 충실히 반영하여 작성합니다.
			    * `userComment`에서 강조된 부분을 특히 신경 써주세요.
			4.  **스타일**:
			    * `option`에 명시된 이모지 사용 여부와 말투를 일관되게 적용합니다.
			    * 전문적이면서도 이해하기 쉬운 언어를 사용합니다.
			
			**이제 위의 정보를 바탕으로 완성된 기술 블로그 포스트를 Markdown 형식으로 작성해주세요.
			**
			""";
}
