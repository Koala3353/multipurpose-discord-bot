package com.general_hello.commands.commands.RankingSystem;

import com.general_hello.commands.Database.SQLiteDataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class RankingSystem
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RankingSystem.class);
    private static DataObject colorData = null;
    private static Font FONT;

    private static final int CARD_WIDTH = 1200;
    private static final int CARD_HEIGHT = CARD_WIDTH / 4;
    private static final int CARD_RATIO = CARD_WIDTH / CARD_HEIGHT;

    private static final float FONT_SIZE = 60f;
    private static final float DISCRIMINATOR_FONT_SIZE = (float) (FONT_SIZE / 1.5);

    private static final int RAW_AVATAR_SIZE = 512;
    private static final int RAW_AVATAR_BORDER_SIZE = RAW_AVATAR_SIZE / 64;

    private static final int BORDER_SIZE = CARD_HEIGHT / 13;

    private static final int AVATAR_SIZE = CARD_HEIGHT - BORDER_SIZE * 2;

    private static final int XP_BAR_WIDTH = CARD_WIDTH - AVATAR_SIZE - BORDER_SIZE * 3;
    private static final int XP_BAR_HEIGHT = CARD_HEIGHT / 5;

    static
    {
        try
        {
            FONT = Font.createFont(Font.TRUETYPE_FONT, RankingSystem.class.getResourceAsStream("/assets/fonts/NotoSans.ttf"));
            colorData = DataObject.fromJson(RankingSystem.class.getResourceAsStream("/assets/wildcards/ColorInfo.json"));
        } catch (FontFormatException | IOException e)
        {
            LOGGER.error("Couldn't load font from resources", e);
        }
    }

    /**
     * returns the relative XP needed to level up to the next level
     *
     * @param currentLevel the current level
     * @return relative XP needed to level up
     */


public static long getXPToLevelUp(int currentLevel)
    {
        double x = 5 * (currentLevel * currentLevel) + (50 * currentLevel) + 100;
        return (long) x;
    }


    /**
     * returns the current level
     *
     * @param totalXP total xp
     * @return the level
     */

    public static int getLevel(long totalXP)
    {
        if (totalXP < 100) return 0;
        int counter = 0;
        long total = 0L;
        while (true)
        {
            long neededForNextLevel = getXPToLevelUp(counter);
            if (neededForNextLevel > totalXP) return counter;
            total += neededForNextLevel;
            if (total > totalXP) return counter;
            counter++;
        }

    }
/**
     * returns the total xp needed to reach a certain level
     *
     * @param level the level
     * @return total xp needed to reach that level
     */

    public static long getTotalXPNeeded(int level)
    {
        long x = 0;
        for (int i = 0; i < level; i++)
        {
            x += getXPToLevelUp(i);
        }
        return x;
    }

    public static long getTotalXP(@Nonnull Connection connection, long guildID, long userID)
    {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM levels WHERE guildID = ? AND userID = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getLong("totalXP");
            }
            return 0L;
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get total xp! (guild " + guildID + ", user " + userID + ")", ex);
            return -1L;
        }
    }

    public static long getTotalXP(long guildID, long userID) throws SQLException {
        Connection connection = SQLiteDataSource.getConnection();
        if (connection == null) return -1L;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM levels WHERE guildID = ? AND userID = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getLong("totalXP");
            }
            return 0L;
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get total xp! (guild " + guildID + ", user " + userID + ")", ex);
            return -1L;
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void addXP(long guildID, long userID, long addedAmount, String name, String discriminator) throws SQLException {
        Connection connection = SQLiteDataSource.getConnection();
        if (connection == null)
        {
            LOGGER.error("Could not get connection from db pool!", new SQLException("Connection == null!"));
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO levels (guildID, userID, totalXP, name, discriminator) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?"))
        {
            long totalXP = getTotalXP(connection, guildID, userID) + addedAmount;
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ps.setLong(3, totalXP);
            ps.setString(4, name);
            ps.setString(5, discriminator);
            ps.setLong(6, totalXP);
            ps.setString(7, name);
            ps.setString(8, discriminator);
            ps.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void addXP(@Nonnull Connection connection, long guildID, long userID, long addedAmount, String name, String discriminator)
    {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO levels (guildID, userID, totalXP, name, discriminator) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?"))
        {
            long totalXP = getTotalXP(connection, guildID, userID) + addedAmount;
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ps.setLong(3, totalXP);
            ps.setString(4, name);
            ps.setString(5, discriminator);
            ps.setLong(6, totalXP);
            ps.setString(7, name);
            ps.setString(8, discriminator);
            ps.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        }
    }

    public static void setXP(long guildID, long userID, long setAmount, String name, String discriminator) throws SQLException {
        Connection connection = SQLiteDataSource.getConnection();
        if (connection == null)
        {
            LOGGER.error("Could not get connection from db pool!", new SQLException("Connection == null!"));
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO levels (guildID, userID, totalXP, name, discriminator) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ps.setLong(3, setAmount);
            ps.setString(4, name);
            ps.setString(5, discriminator);
            ps.setLong(6, setAmount);
            ps.setString(7, name);
            ps.setString(8, discriminator);
            ps.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void setXP(@Nonnull Connection connection, long guildID, long userID, long setAmount, String name, String discriminator)
    {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO levels (guildID, userID, totalXP, name, discriminator) values (?,?,?,?,?) ON DUPLICATE KEY UPDATE totalXP = ?, name = ?, discriminator = ?"))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ps.setLong(3, setAmount);
            ps.setString(4, name);
            ps.setString(5, discriminator);
            ps.setLong(6, setAmount);
            ps.setString(7, name);
            ps.setString(8, discriminator);
            ps.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Could not add XP!", e);
        }
    }

    public static String getPreferredCard(@Nonnull User user) throws SQLException {
        Connection connection = SQLiteDataSource.getConnection();
        if (connection == null) return "card1";
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM wildcardSettings WHERE userID = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("card") : "card1";
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user " + user.getIdLong() + ")", ex);
            return "card1";
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static void setPreferredCard(@Nonnull User user, @Nonnull String card) throws SQLException {
        Connection connection = SQLiteDataSource.getConnection();
        if (connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO wildcardSettings (userID, card) VALUES (?,?) ON DUPLICATE KEY UPDATE card = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ps.setString(2, card);
            ps.setString(3, card);
            ps.execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user " + user.getIdLong() + ")", ex);
        } finally
        {
            Util.closeQuietly(connection);
        }
    }

    public static String getPreferredCard(@Nonnull Connection connection, @Nonnull User user)
    {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM wildcardSettings WHERE userID = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("card") : "card1";
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user " + user.getIdLong() + ")", ex);
            return "card1";
        }
    }

    public static void setPreferredCard(@Nonnull Connection connection, @Nonnull User user, @Nonnull String card)
    {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO wildcardSettings (userID, card) VALUES (?,?) ON DUPLICATE KEY UPDATE card = ?"))
        {
            ps.setLong(1, user.getIdLong());
            ps.setString(2, card);
            ps.execute();
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get user preferred wildcard background (user " + user.getIdLong() + ")", ex);
        }
    }

    public static byte[] generateLevelCard(@Nonnull User user, @Nonnull Guild guild)
    {
        try
        {
            BufferedImage avatar = ImageIO.read(new URL(user.getEffectiveAvatarUrl() + "?size=" + RAW_AVATAR_SIZE));

            // make the avatar round
            BufferedImage roundAvatar = new BufferedImage(RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D roundAvatarG = roundAvatar.createGraphics();
            roundAvatarG.setColor(Color.white);
            roundAvatarG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            roundAvatarG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            roundAvatarG.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            roundAvatarG.fillArc(0, 0, RAW_AVATAR_SIZE, RAW_AVATAR_SIZE, 0, 360);
            int rawAvatarSize = RAW_AVATAR_SIZE - RAW_AVATAR_BORDER_SIZE * 2;
            roundAvatarG.setClip(new Ellipse2D.Float(RAW_AVATAR_BORDER_SIZE, RAW_AVATAR_BORDER_SIZE, rawAvatarSize, rawAvatarSize));
            roundAvatarG.drawImage(avatar, RAW_AVATAR_BORDER_SIZE, RAW_AVATAR_BORDER_SIZE, rawAvatarSize, rawAvatarSize, null);
            roundAvatarG.dispose();

            // downscale the avatar to get rid of sharp edges
            BufferedImage downscaledAvatar = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D downscaledAvatarG = downscaledAvatar.createGraphics();
            downscaledAvatarG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            downscaledAvatarG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            downscaledAvatarG.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            downscaledAvatarG.drawImage(roundAvatar, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null);
            downscaledAvatarG.dispose();

            // prepare level card
            String card = getPreferredCard(user);
            BufferedImage rankCard = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = rankCard.createGraphics();

            InputStream background = RankingSystem.class.getResourceAsStream("/assets/wildcards/" + card + ".png");
            if (background != null)
            {
                BufferedImage image = makeRoundedCorner(ImageIO.read(background), 60);
                int width = image.getWidth();
                int height = image.getHeight();
                int drawWidth = 0;
                int drawHeight = 0;
                if (width > height)
                {
                    drawWidth = width;
                    drawHeight = width / CARD_RATIO;
                } else
                {
                    drawHeight = height;
                    drawWidth = height * CARD_RATIO;
                }
                g.drawImage(image.getSubimage(0, 0, drawWidth, drawHeight), 0, 0, CARD_WIDTH, CARD_HEIGHT, null);
            }

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

            g.drawImage(downscaledAvatar, BORDER_SIZE, BORDER_SIZE, AVATAR_SIZE, AVATAR_SIZE, null);

            // draw username
            g.setFont(FONT.deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
            g.setColor(Color.white);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            String userString = user.getName();
            int leftAvatarAlign = AVATAR_SIZE + BORDER_SIZE * 2;
            g.drawString(userString, leftAvatarAlign, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT);

            // draw discriminator
            int nameWidth = g.getFontMetrics().stringWidth(userString);
            g.setFont(g.getFont().deriveFont(DISCRIMINATOR_FONT_SIZE).deriveFont(Font.BOLD));
            g.setColor(g.getColor().darker().darker());
            g.drawString("#" + user.getDiscriminator(), AVATAR_SIZE + BORDER_SIZE * 2 + nameWidth, CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT);

            // draw xp
            long totalXP = getTotalXP(guild.getIdLong(), user.getIdLong());
            int currentLevel = getLevel(totalXP);
            long neededXP = getXPToLevelUp(currentLevel);
            long currentXP = totalXP - getTotalXPNeeded(currentLevel);
            // draw level
            Color c = getColor(card);
            g.setFont(g.getFont().deriveFont(FONT_SIZE).deriveFont(Font.BOLD));
            g.setColor(Color.white);
            String levelString = "Level " + currentLevel;
            g.drawString(levelString, CARD_WIDTH - BORDER_SIZE - g.getFontMetrics().stringWidth(levelString), CARD_HEIGHT - BORDER_SIZE * 2 - XP_BAR_HEIGHT);
            g.setColor(c);
            // draw empty xp bar
            g.setColor(c.darker().darker());
            g.fillRoundRect(leftAvatarAlign, CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE, XP_BAR_WIDTH, XP_BAR_HEIGHT, XP_BAR_HEIGHT, XP_BAR_HEIGHT);

            // draw current xp bar
            g.setColor(c);
            int progress = (int) (((double) currentXP) / neededXP * XP_BAR_WIDTH);
            if (progress < 50 && progress != 0) progress = 50;
            g.fillRoundRect(leftAvatarAlign, CARD_HEIGHT - XP_BAR_HEIGHT - BORDER_SIZE, progress, XP_BAR_HEIGHT, XP_BAR_HEIGHT, XP_BAR_HEIGHT);
            int rank = getRank(guild.getIdLong(), user.getIdLong());
            Color rankColor = getRankColor(rank);
            g.setColor(rankColor == null ? c.brighter() : rankColor);
            String rankString = "#" + rank;
            int rankWidth = g.getFontMetrics().stringWidth(rankString);
            g.drawString(rankString, CARD_WIDTH - BORDER_SIZE - rankWidth, 70);
            g.setFont(g.getFont().deriveFont(DISCRIMINATOR_FONT_SIZE - 5).deriveFont(Font.BOLD));
            g.drawString("Rank", CARD_WIDTH - BORDER_SIZE - rankWidth - g.getFontMetrics().stringWidth("Rank") - 10, 70);
            g.setFont(g.getFont().deriveFont(FONT_SIZE / 2).deriveFont(Font.BOLD));
            g.setColor(Color.white);
            String xpString = formatXP(currentXP) + " / " + formatXP(neededXP) + " XP";
            int xpXPos = ((leftAvatarAlign + CARD_WIDTH - BORDER_SIZE) / 2) - (g.getFontMetrics().stringWidth(xpString) / 2);
            g.drawString(xpString, xpXPos, CARD_HEIGHT - BORDER_SIZE - 18);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(rankCard, "png", baos);
            return baos.toByteArray();
        } catch (IOException | SQLException e)
        {
            LOGGER.error("Error while generating level cards", e);
        }
        return null;
    }

    private static final String[] suffix = new String[]{"", "k", "M", "G", "T"};
    private static final int MAX_LENGTH = 5;

    public static String formatXP(long xp)
    {
        String r = new DecimalFormat("##0E0").format(xp);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while (r.length() > MAX_LENGTH || r.matches("[0-9]+,[a-z]"))
        {
            r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
        }
        return r.replaceAll(",", ".");
    }

    public static Color getColor(String fileName)
    {
        return Color.decode("#" + colorData.getString(fileName, String.valueOf(String.class)));
    }

    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    public static ArrayList<RankedUser> getTopTen(long guildID) throws SQLException {
        String sql = "SELECT * FROM levels WHERE guildID = ? ORDER by totalXP DESC LIMIT 10";
        Connection connection = SQLiteDataSource.getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, guildID);
            ResultSet rs = ps.executeQuery();
            ArrayList<RankedUser> rankedUsers = new ArrayList<>();
            while (rs.next())
            {
                long userID = rs.getLong("userID");
                long totalXP = rs.getLong("totalXP");
                String name = rs.getString("name");
                String discriminator = rs.getString("discriminator");
                rankedUsers.add(new RankedUser(guildID, userID, totalXP, name, discriminator));
            }
            return rankedUsers;
        } catch (SQLException ex)
        {
            LOGGER.error("Could not get leaderboard for guild " + guildID + "!", ex);
            return new ArrayList<>();
        } finally
        {
            Util.closeQuietly(connection);
        }
    }


    public static int getRank(long guildID, long userID) throws SQLException {
        String qry = "SELECT 0, FIND_IN_SET(totalXP, (SELECT GROUP_CONCAT(DISTINCT totalXP ORDER BY totalXP DESC) FROM levels ))" +
                " AS rank FROM levels WHERE guildID = ? AND userID = ?";
        Connection connection = SQLiteDataSource.getConnection();
        if (connection == null) return -1;
        try (PreparedStatement ps = connection.prepareStatement(qry))
        {
            ps.setLong(1, guildID);
            ps.setLong(2, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("rank");
            return -1;
        } catch (SQLException exception)
        {
            return -1;
        } finally
        {
            Util.closeQuietly(connection);
        }

    }

    public static Color getRankColor(int rank)
    {
        switch (rank)
        {
            case 1:
                return Color.decode("#D4AF37");
            case 2:
                return Color.decode("#BEC2CB");
            case 3:
                return Color.decode("#CD7F32");
            default:
                return null;
        }
    }


}
