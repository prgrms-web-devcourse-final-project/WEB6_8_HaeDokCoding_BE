-- 테이블 생성
CREATE TABLE IF NOT EXISTS cocktail (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        cocktail_name VARCHAR(255),
    cocktail_name_ko VARCHAR(255),
    alcohol_strength VARCHAR(50),
    cocktail_story CLOB,
    cocktail_type VARCHAR(50),
    alcohol_base_type VARCHAR(50),
    ingredient CLOB,
    recipe CLOB,
    cocktail_img_url VARCHAR(500)
    );

-- CSV 파일에서 데이터 읽어오기
INSERT INTO cocktail (
    cocktail_name, cocktail_name_ko, alcohol_strength,
    cocktail_story, cocktail_type, alcohol_base_type,
    ingredient, recipe, cocktail_img_url
)
SELECT
    cocktailName,
    cocktailNameKo,
    alcoholStrength,
    cocktailStory,
    cocktailType,
    alcoholBaseType,
    ingredient,
    recipe,
    cocktailImgUrl
FROM CSVREAD(
        'src/main/resources/cocktails.csv',   -- 파일 경로
        NULL,                                  -- 헤더 사용 여부 (NULL이면 첫 줄을 컬럼명으로 인식)
        'charset=UTF-8 fieldSeparator=, fieldDelimiter="'
     );

MERGE INTO CATEGORY KEY(ID) VALUES
    (1, '전체', '전체게시판'),
    (2, '레시피', '레시피게시판'),
    (3, '팁', '팁게시판'),
    (4, '질문', '질문게시판'),
    (5, '자유', '자유게시판');
