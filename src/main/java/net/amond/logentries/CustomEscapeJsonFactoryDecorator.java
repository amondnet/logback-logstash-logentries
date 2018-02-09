package net.amond.logentries;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import net.logstash.logback.decorate.JsonFactoryDecorator;

public class CustomEscapeJsonFactoryDecorator implements JsonFactoryDecorator {

  private static class CustomCharacterEscapes extends CharacterEscapes {

    private static final SerializedString ESCAPE_LF = new SerializedString("\u2028");
    private static final SerializedString ESCAPE_CR = new SerializedString("");
    private static final SerializedString ESCAPE_TAB = new SerializedString("");

    private final int[] escapeCodesForAscii;

    private CustomCharacterEscapes() {
      escapeCodesForAscii = standardAsciiEscapesForJSON();
      escapeCodesForAscii[(int) '\n'] = ESCAPE_CUSTOM;
      escapeCodesForAscii[(int) '\r'] = ESCAPE_CUSTOM;
      escapeCodesForAscii[(int) '\t'] = ESCAPE_CUSTOM;
    }

    @Override
    public SerializableString getEscapeSequence(int ch) {
      switch (ch) {
        case '\t':
          return ESCAPE_TAB;
        case '\n':
          return ESCAPE_LF;
        case '\r':
          /*
           * Don't output anything for carriage return, since we're
           * collapsing \r\n into just \u2028.
           */
          return ESCAPE_CR;
        default:
          /*
           * For all other chars, return a non-escaped string.
           * This shouldn't be called since the generator uses
           * JsonGenerator.Feature.ESCAPE_NON_ASCII,
           *
           */
          return new SerializedString(new String(new char[] {(char) ch}));
      }
    }

    @Override
    public int[] getEscapeCodesForAscii() {
      return escapeCodesForAscii;
    }
  }

  @Override
  public MappingJsonFactory decorate(MappingJsonFactory factory) {
    factory.setCharacterEscapes(new CustomCharacterEscapes());
    return factory;
  }
}