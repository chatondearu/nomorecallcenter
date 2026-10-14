CREATE TABLE "personal_rules" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
	"user_id" uuid NOT NULL,
	"pattern" text NOT NULL,
	"match_type" text NOT NULL,
	"ttl_preset" text NOT NULL,
	"expires_at" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "personal_rules_user_pattern_match" UNIQUE("user_id","pattern","match_type")
);
--> statement-breakpoint
CREATE TABLE "proposals" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
	"pattern" text NOT NULL,
	"match_type" text NOT NULL,
	"status" text DEFAULT 'pending' NOT NULL,
	"proposed_by" uuid NOT NULL,
	"personal_rule_id" uuid,
	"reviewed_by" uuid,
	"reviewed_at" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "personal_rules" ADD CONSTRAINT "personal_rules_user_id_users_id_fk" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "proposals" ADD CONSTRAINT "proposals_proposed_by_users_id_fk" FOREIGN KEY ("proposed_by") REFERENCES "auth"."users"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "proposals" ADD CONSTRAINT "proposals_personal_rule_id_personal_rules_id_fk" FOREIGN KEY ("personal_rule_id") REFERENCES "public"."personal_rules"("id") ON DELETE set null ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "proposals" ADD CONSTRAINT "proposals_reviewed_by_users_id_fk" FOREIGN KEY ("reviewed_by") REFERENCES "auth"."users"("id") ON DELETE set null ON UPDATE no action;