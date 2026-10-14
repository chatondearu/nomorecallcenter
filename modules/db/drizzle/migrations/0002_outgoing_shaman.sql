CREATE TABLE "list_entries" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
	"pattern" text NOT NULL,
	"source" text NOT NULL,
	"attribution" text NOT NULL,
	"source_date" text NOT NULL,
	"active" boolean DEFAULT true NOT NULL,
	"revision" bigint NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "list_entries_pattern_unique" UNIQUE("pattern")
);
