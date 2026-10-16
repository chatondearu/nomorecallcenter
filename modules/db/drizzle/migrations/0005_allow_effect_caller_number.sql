ALTER TABLE "personal_rules" DROP CONSTRAINT "personal_rules_user_pattern_match";--> statement-breakpoint
ALTER TABLE "call_events" ADD COLUMN "caller_number" text;--> statement-breakpoint
ALTER TABLE "personal_rules" ADD COLUMN "effect" text DEFAULT 'block' NOT NULL;--> statement-breakpoint
ALTER TABLE "personal_rules" ADD CONSTRAINT "personal_rules_user_pattern_match_effect" UNIQUE("user_id","pattern","match_type","effect");