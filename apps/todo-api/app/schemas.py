from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field, field_validator


class TodoBase(BaseModel):
    title: str
    description: Optional[str] = None
    completed: bool = False

    @field_validator("title")
    @classmethod
    def title_must_not_be_blank(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("Title is required")
        return v


class TodoCreate(TodoBase):
    pass


class TodoUpdate(TodoBase):
    pass


class TodoRead(BaseModel):
    model_config = ConfigDict(from_attributes=True, populate_by_name=True)

    id: int
    title: str
    description: Optional[str] = None
    completed: bool
    created_at: datetime = Field(alias="createdAt")
    updated_at: Optional[datetime] = Field(default=None, alias="updatedAt")
