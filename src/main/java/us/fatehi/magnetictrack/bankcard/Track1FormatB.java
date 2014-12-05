/*
 *
 * Magnetic Track Parser
 * https://github.com/sualeh/magnetictrackparser
 * Copyright (c) 2014, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */
package us.fatehi.magnetictrack.bankcard;


import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.fatehi.creditcardnumber.AccountNumber;
import us.fatehi.creditcardnumber.ExpirationDate;
import us.fatehi.creditcardnumber.Name;
import us.fatehi.creditcardnumber.PrimaryAccountNumber;
import us.fatehi.creditcardnumber.ServiceCode;

/**
 * Parses, and represents a card's track 1 data, in format "B". From <a
 * href="https://en.wikipedia.org/wiki/ISO/IEC_7813#Magnetic_tracks"
 * >Wikipedia - ISO/IEC 7813</a><br/>
 * The Track 1 structure is specified as:
 * <ol>
 * <li>STX: Start sentinel "%"</li>
 * <li>FC: Format code "B" (The format described here. Format "A" is
 * reserved for proprietary use.)</li>
 * <li>PAN: Primary Account Number, up to 19 digits</li>
 * <li>FS: Separator "^"</li>
 * <li>NM: Name, 2 to 26 characters (including separators, where
 * appropriate, between surname, first name etc.)</li>
 * <li>FS: Separator "^"</li>
 * <li>ED: Expiration data, 4 digits or "^"</li>
 * <li>SC: Service code, 3 digits or "^"</li>
 * <li>DD: Discretionary data, balance of characters</li>
 * <li>ETX: End sentinel "?"</li>
 * <li>LRC: Longitudinal redundancy check, calculated according to
 * ISO/IEC 7811-2</li>
 * </ol>
 * The maximum record length is 79 alphanumeric characters.
 * 
 * @see <a
 *      href="https://en.wikipedia.org/wiki/ISO/IEC_7813#Magnetic_tracks">Wikipedia
 *      - ISO/IEC 7813</a>
 */
public class Track1FormatB
  extends BaseBankCardTrackData
{

  private static final long serialVersionUID = 3020739300944280022L;

  private static final Pattern track1FormatBPattern = Pattern
    .compile("(%?([A-Z])([0-9]{1,19})\\^([^\\^]{2,26})\\^([0-9]{4}|\\^)([0-9]{3}|\\^)?([^\\?]+)?\\??)[\t\n\r ]{0,2}.*");

  /**
   * Parses magnetic track 1 format B data into a Track1FormatB object.
   * 
   * @param rawTrackData
   *        Raw track data as a string. Can include newlines, and other
   *        tracks as well.
   * @return A Track1FormatB instance, corresponding to the parsed data.
   */
  public static Track1FormatB from(final String rawTrackData)
  {
    final Matcher matcher = track1FormatBPattern
      .matcher(trimToEmpty(rawTrackData));

    final String rawTrack1Data;
    final String discretionaryData;
    if (matcher.matches())
    {
      rawTrack1Data = getGroup(matcher, 1);
      discretionaryData = getGroup(matcher, 7);
    }
    else
    {
      rawTrack1Data = "";
      discretionaryData = "";
    }
    return new Track1FormatB(rawTrack1Data, discretionaryData, matcher);
  }

  private final String formatCode;
  private final PrimaryAccountNumber pan;
  private final Name name;
  private final ExpirationDate expirationDate;
  private final ServiceCode serviceCode;

  private Track1FormatB(final String rawTrack1Data,
                        final String discretionaryData,
                        final Matcher matcher)
  {
    super(rawTrack1Data, discretionaryData);

    if (matcher.matches())
    {
      formatCode = getGroup(matcher, 2);
      pan = new AccountNumber(getGroup(matcher, 3));
      name = new Name(getGroup(matcher, 4));
      expirationDate = new ExpirationDate(getGroup(matcher, 5));
      serviceCode = new ServiceCode(getGroup(matcher, 6));

    }
    else
    {
      formatCode = "";
      pan = new AccountNumber();
      name = new Name();
      expirationDate = new ExpirationDate();
      serviceCode = new ServiceCode();
    }
  }

  /**
   * @see us.fatehi.magnetictrack.TrackData#exceedsMaximumLength()
   */
  @Override
  public boolean exceedsMaximumLength()
  {
    return hasRawTrackData() && getRawTrackData().length() > 79;
  }

  /**
   * Gets the card expiration date.
   * 
   * @return Card expiration date.
   */
  public ExpirationDate getExpirationDate()
  {
    return expirationDate;
  }

  /**
   * Gets the track 1 format code, usually "B".
   * 
   * @return Track 1 format code, usually "B"
   */
  public String getFormatCode()
  {
    return formatCode;
  }

  /**
   * Gets the cardholder's name.s
   * 
   * @return Cardholder's name
   */
  public Name getName()
  {
    return name;
  }

  /**
   * Gets the primary account number for the card.
   * 
   * @return Primary account number.
   */
  public PrimaryAccountNumber getPrimaryAccountNumber()
  {
    return pan;
  }

  /**
   * Gets the card service code.
   * 
   * @return Card service code.
   */
  public ServiceCode getServiceCode()
  {
    return serviceCode;
  }

  /**
   * Checks whether the card expiration date is available.
   * 
   * @return True if the card expiration date is available.
   */
  public boolean hasExpirationDate()
  {
    return expirationDate != null && expirationDate.hasExpirationDate();
  }

  /**
   * Checks whether the format code is available.
   * 
   * @return True if the format code is available.
   */
  public boolean hasFormatCode()
  {
    return !isBlank(formatCode);
  }

  /**
   * Checks whether the cardholder's name is available.
   * 
   * @return True if the cardholder's name is available.
   */
  public boolean hasName()
  {
    return name != null && name.hasName();
  }

  /**
   * Checks whether the primary account number for the card is
   * available.
   * 
   * @return True if the primary account number for the card is
   *         available.
   */
  public boolean hasPrimaryAccountNumber()
  {
    return pan != null;
  }

  /**
   * Checks whether the card service code is available.
   * 
   * @return True if the card service code is available.
   */
  public boolean hasServiceCode()
  {
    return serviceCode != null && serviceCode.hasServiceCode();
  }

}
