package sura.pruebalegoback.sanitizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringSanitizerServiceTest {

  private StringSanitizerService sanitizerService;

  @BeforeEach
  public void setup() {
    sanitizerService = new StringSanitizerService();
  }

  @Test
  public void sanitizeString_shouldEscapeHtmlCharacters() {
    // Arrange
    String input = "<script>alert('XSS')</script>";

    // Act
    String sanitizedString = sanitizerService.sanitizeString(input);

    // Assert
    assertThat(sanitizedString).isEqualTo("&lt;script&gt;alert('XSS')&lt;/script&gt;");
  }

  @Test
  public void sanitizeString_shouldReturnNullForNullInput() {
    // Arrange
    String input = null;

    // Act
    String sanitizedString = sanitizerService.sanitizeString(input);

    // Assert
    assertThat(sanitizedString).isNull();
  }

  @Test
  public void sanitizeArray_shouldEscapeHtmlCharactersInArray() {
    // Arrange
    String[] inputs = { "<script>alert('XSS')</script>", "Hello, World!" };

    // Act
    String[] sanitizedArray = sanitizerService.sanitizeArray(inputs);

    // Assert
    assertThat(sanitizedArray).containsExactly("&lt;script&gt;alert('XSS')&lt;/script&gt;", "Hello, World!");
  }

  @Test
  public void sanitizeArray_shouldReturnNullForNullInput() {
    // Arrange
    String[] inputs = null;

    // Act
    String[] sanitizedArray = sanitizerService.sanitizeArray(inputs);

    // Assert
    assertThat(sanitizedArray).isNull();
  }

  @Test
  public void sanitizeDto_shouldEscapeHtmlCharactersInDtoFields() {
    // Arrange
    TestDto dto = new TestDto("<script>alert('XSS')</script>", "Hello, World!");

    // Act
    TestDto sanitizedDto = sanitizerService.sanitizeDto(dto);

    // Assert
    assertThat(sanitizedDto.getField1()).isEqualTo("&lt;script&gt;alert('XSS')&lt;/script&gt;");
    assertThat(sanitizedDto.getField2()).isEqualTo("Hello, World!");
  }

  @Test
  public void sanitizeDto_shouldReturnNullForNullDto() {
    // Arrange
    TestDto dto = null;

    // Act
    TestDto sanitizedDto = sanitizerService.sanitizeDto(dto);

    // Assert
    assertThat(sanitizedDto).isNull();
  }

  private static class TestDto {
    private String field1;
    private String field2;

    public TestDto(String field1, String field2) {
      this.field1 = field1;
      this.field2 = field2;
    }

    public String getField1() {
      return field1;
    }

    public String getField2() {
      return field2;
    }
  }
}