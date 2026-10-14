CREATE TABLE "call_events" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
	"user_id" uuid NOT NULL,
	"prefix" text,
	"number_hash" text,
	"action" text NOT NULL,
	"occurred_at" timestamp with time zone NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "call_events" ADD CONSTRAINT "call_events_user_id_users_id_fk" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
CREATE INDEX "call_events_user_occurred_idx" ON "call_events" USING btree ("user_id","occurred_at");--> statement-breakpoint
CREATE INDEX "call_events_occurred_idx" ON "call_events" USING btree ("occurred_at");