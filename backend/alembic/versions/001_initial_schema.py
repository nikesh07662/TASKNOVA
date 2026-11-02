"""Initial academic workflow database schema

Revision ID: 001_initial_schema
Revises: 
Create Date: 2026-11-02 19:30:00

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa

revision: str = '001_initial_schema'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

def upgrade() -> None:
    # Users table
    op.create_table(
        'users',
        sa.Column('id', sa.String(), primary_key=True),
        sa.Column('email', sa.String(), unique=True, index=True, nullable=False),
        sa.Column('full_name', sa.String(), nullable=False),
        sa.Column('hashed_password', sa.String(), nullable=False),
        sa.Column('is_active', sa.Boolean(), default=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('google_access_token', sa.String(), nullable=True),
        sa.Column('google_refresh_token', sa.String(), nullable=True),
        sa.Column('is_google_connected', sa.Boolean(), default=False),
        sa.Column('microsoft_access_token', sa.String(), nullable=True),
        sa.Column('microsoft_refresh_token', sa.String(), nullable=True),
        sa.Column('is_microsoft_connected', sa.Boolean(), default=False),
        sa.Column('preferred_ai_model', sa.String(), default='gpt-4o'),
        sa.Column('attendance_target', sa.Float(), default=75.0)
    )

    # Assignments table
    op.create_table(
        'assignments',
        sa.Column('id', sa.String(), primary_key=True),
        sa.Column('user_id', sa.String(), sa.ForeignKey('users.id'), index=True, nullable=False),
        sa.Column('title', sa.String(), nullable=False),
        sa.Column('subject', sa.String(), nullable=False),
        sa.Column('instructions', sa.Text(), nullable=True),
        sa.Column('deadline', sa.String(), nullable=False),
        sa.Column('platform', sa.String(), nullable=False),
        sa.Column('status', sa.String(), default='PENDING'),
        sa.Column('source_platform_id', sa.String(), nullable=True),
        sa.Column('matched_document_id', sa.String(), nullable=True),
        sa.Column('matched_document_name', sa.String(), nullable=True),
        sa.Column('is_duplicate', sa.Boolean(), default=False),
        sa.Column('is_readiness_checked', sa.Boolean(), default=False),
        sa.Column('missing_files_warning', sa.String(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True)
    )

    # Cloud files table
    op.create_table(
        'cloud_files',
        sa.Column('id', sa.String(), primary_key=True),
        sa.Column('user_id', sa.String(), sa.ForeignKey('users.id'), index=True, nullable=False),
        sa.Column('name', sa.String(), nullable=False),
        sa.Column('provider', sa.String(), nullable=False),
        sa.Column('mime_type', sa.String(), nullable=True),
        sa.Column('size_bytes', sa.Integer(), default=0),
        sa.Column('modified_time', sa.String(), nullable=True),
        sa.Column('web_view_link', sa.String(), nullable=True),
        sa.Column('download_url', sa.String(), nullable=True),
        sa.Column('path', sa.String(), default='/'),
        sa.Column('is_generated_doc', sa.Boolean(), default=False),
        sa.Column('associated_subject', sa.String(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True)
    )

    # Submissions table
    op.create_table(
        'submissions',
        sa.Column('id', sa.String(), primary_key=True),
        sa.Column('user_id', sa.String(), sa.ForeignKey('users.id'), index=True, nullable=False),
        sa.Column('assignment_id', sa.String(), sa.ForeignKey('assignments.id'), nullable=False),
        sa.Column('document_id', sa.String(), nullable=False),
        sa.Column('document_name', sa.String(), nullable=False),
        sa.Column('destination_platform', sa.String(), nullable=False),
        sa.Column('stage', sa.String(), default='PREPARED_FOR_REVIEW'),
        sa.Column('idempotency_token', sa.String(), unique=True, index=True, nullable=False),
        sa.Column('student_command', sa.String(), nullable=True),
        sa.Column('verification_receipt', sa.String(), nullable=True),
        sa.Column('error_reason', sa.String(), nullable=True),
        sa.Column('audit_trail_json', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('submitted_at', sa.DateTime(), nullable=True)
    )

    # Attendance table
    op.create_table(
        'attendance_courses',
        sa.Column('id', sa.String(), primary_key=True),
        sa.Column('user_id', sa.String(), sa.ForeignKey('users.id'), index=True, nullable=False),
        sa.Column('subject_id', sa.String(), nullable=False),
        sa.Column('subject_name', sa.String(), nullable=False),
        sa.Column('total_lectures', sa.Integer(), default=0),
        sa.Column('attended_lectures', sa.Integer(), default=0),
        sa.Column('missed_lectures', sa.Integer(), default=0),
        sa.Column('target_percentage', sa.Float(), default=75.0),
        sa.Column('last_updated', sa.DateTime(), nullable=True)
    )

    # Activity logs table
    op.create_table(
        'activity_logs',
        sa.Column('id', sa.String(), primary_key=True),
        sa.Column('user_id', sa.String(), sa.ForeignKey('users.id'), index=True, nullable=False),
        sa.Column('title', sa.String(), nullable=False),
        sa.Column('description', sa.Text(), nullable=False),
        sa.Column('category', sa.String(), nullable=False),
        sa.Column('is_success', sa.Boolean(), default=True),
        sa.Column('related_entity_id', sa.String(), nullable=True),
        sa.Column('timestamp', sa.DateTime(), nullable=True)
    )

    # Task memory items table
    op.create_table(
        'task_memory_items',
        sa.Column('id', sa.String(), primary_key=True),
        sa.Column('user_id', sa.String(), sa.ForeignKey('users.id'), index=True, nullable=False),
        sa.Column('title', sa.String(), nullable=False),
        sa.Column('summary', sa.Text(), nullable=False),
        sa.Column('content', sa.Text(), nullable=False),
        sa.Column('category', sa.String(), nullable=False),
        sa.Column('related_entity_id', sa.String(), nullable=True),
        sa.Column('metadata_json', sa.Text(), nullable=True),
        sa.Column('embedding_json', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.Column('updated_at', sa.DateTime(), nullable=True)
    )

def downgrade() -> None:
    op.drop_table('task_memory_items')
    op.drop_table('activity_logs')
    op.drop_table('attendance_courses')
    op.drop_table('submissions')
    op.drop_table('cloud_files')
    op.drop_table('assignments')
    op.drop_table('users')
