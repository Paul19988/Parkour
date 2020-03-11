package io.github.a5h73y.conversation;

import io.github.a5h73y.Parkour;
import io.github.a5h73y.course.CourseInfo;
import io.github.a5h73y.utilities.DatabaseMethods;
import io.github.a5h73y.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class LeaderboardConversation extends ParkourConversation {

    public LeaderboardConversation(Player player) {
        super(player);
    }

    @Override
    public Prompt getEntryPrompt() {
        return new ChooseCourse();
    }

    private class ChooseCourse extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.LIGHT_PURPLE + " Which course would you like to view?";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String message) {
            if (!CourseInfo.getAllCourses().contains(message.toLowerCase())) {
                ParkourConversation.sendErrorMessage(context, "This course does not exist");
                return this;
            }

            context.setSessionData("course", message.toLowerCase());
            return new ChooseType();
        }
    }

    private class ChooseType extends FixedSetPrompt {

        ChooseType() {
            super("personal", "global");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.LIGHT_PURPLE + " What type of leaderboards would you like to see?\n" +
                    ChatColor.GREEN + formatFixedSet();
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String choice) {
            context.setSessionData("type", choice);

            return new ChooseAmount();
        }
    }

    private class ChooseAmount extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.LIGHT_PURPLE + " How many results would you like?";
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.intValue() > 0 && input.intValue() <= Parkour.getSettings().getLeaderboardMaxEntries();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return "Amount must be between 1 and " + Parkour.getSettings().getLeaderboardMaxEntries() + ".";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number amount) {
            context.setSessionData("amount", amount.intValue());

            return new DisplayLeaderboards();
        }
    }

    private class DisplayLeaderboards extends MessagePrompt {
        public String getPromptText(ConversationContext context) {
            final String leaderboardType = (String) context.getSessionData("type");
            final String courseName = (String) context.getSessionData("course");
            final Integer amount = (Integer) context.getSessionData("amount");
            final Player player = Bukkit.getPlayer((String) context.getSessionData("playerName"));

            Bukkit.getScheduler().runTaskLaterAsynchronously(Parkour.getInstance(), () -> {
                if (leaderboardType.equals("personal")) {
                    Utils.displayLeaderboard(player, DatabaseMethods.getTopPlayerCourseResults(player.getName(), courseName, amount), courseName);
                } else if (leaderboardType.equals("global")) {
                    Utils.displayLeaderboard(player, DatabaseMethods.getTopCourseResults(courseName, amount), courseName);
                }
            }, 3);

            return "";
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return Prompt.END_OF_CONVERSATION;
        }
    }
}