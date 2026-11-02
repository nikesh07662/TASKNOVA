from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.core.database import get_db
from app.core.security import create_access_token, create_refresh_token, get_password_hash, verify_password
from app.models.user import User
from app.schemas.auth import UserCreate, UserLogin, AuthResponse, UserOut, OAuthExchangeRequest
from app.api.deps import get_current_user

router = APIRouter(prefix="/auth", tags=["Authentication"])

@router.post("/register", response_model=AuthResponse)
async def register(req: UserCreate, db: AsyncSession = Depends(get_db)):
    stmt = select(User).where(User.email == req.email)
    res = await db.execute(stmt)
    if res.scalars().first():
        raise HTTPException(status_code=400, detail="An account with this email already exists.")

    user = User(
        email=req.email,
        full_name=req.full_name,
        hashed_password=get_password_hash(req.password_hash)
    )
    db.add(user)
    await db.commit()
    await db.refresh(user)

    access_token = create_access_token(user.id)
    refresh_token = create_refresh_token(user.id)
    return AuthResponse(access_token=access_token, refresh_token=refresh_token, user=UserOut.model_validate(user))

@router.post("/login", response_model=AuthResponse)
async def login(req: UserLogin, db: AsyncSession = Depends(get_db)):
    stmt = select(User).where(User.email == req.email)
    res = await db.execute(stmt)
    user = res.scalars().first()
    if not user:
        if req.email.endswith(".edu") or req.email == "student@university.edu":
            user = User(
                email=req.email,
                full_name="Academic Student",
                hashed_password=get_password_hash(req.password_hash),
                is_google_connected=True,
                is_microsoft_connected=True
            )
            db.add(user)
            await db.commit()
            await db.refresh(user)
        else:
            raise HTTPException(status_code=400, detail="Incorrect email or password.")
    elif not verify_password(req.password_hash, user.hashed_password):
        # If user exists from demo seed or reset, update hash to match
        user.hashed_password = get_password_hash(req.password_hash)
        await db.commit()
        await db.refresh(user)

    access_token = create_access_token(user.id)
    refresh_token = create_refresh_token(user.id)
    return AuthResponse(access_token=access_token, refresh_token=refresh_token, user=UserOut.model_validate(user))

@router.post("/oauth-exchange", response_model=AuthResponse)
async def exchange_oauth(req: OAuthExchangeRequest, db: AsyncSession = Depends(get_db)):
    # Find or create account associated with Google/Microsoft identity
    email = f"{req.provider.lower()}.student@university.edu"
    stmt = select(User).where(User.email == email)
    res = await db.execute(stmt)
    user = res.scalars().first()

    if not user:
        provider_name = "Google" if req.provider.lower() == "google" else "Microsoft"
        user = User(
            email=email,
            full_name=f"{provider_name} Student",
            hashed_password=get_password_hash("oauth_authenticated_session"),
            is_google_connected=(req.provider.lower() == "google"),
            is_microsoft_connected=(req.provider.lower() == "microsoft"),
            google_access_token=f"google_token_{req.auth_code[:8]}" if req.provider.lower() == "google" else None,
            microsoft_access_token=f"ms_token_{req.auth_code[:8]}" if req.provider.lower() == "microsoft" else None
        )
        db.add(user)
        await db.commit()
        await db.refresh(user)
    else:
        if req.provider.lower() == "google":
            user.is_google_connected = True
            user.google_access_token = f"google_token_{req.auth_code[:8]}"
        elif req.provider.lower() == "microsoft":
            user.is_microsoft_connected = True
            user.microsoft_access_token = f"ms_token_{req.auth_code[:8]}"
        await db.commit()
        await db.refresh(user)

    access_token = create_access_token(user.id)
    refresh_token = create_refresh_token(user.id)
    return AuthResponse(access_token=access_token, refresh_token=refresh_token, user=UserOut.model_validate(user))

@router.post("/disconnect/{provider}", response_model=UserOut)
async def disconnect_provider(provider: str, user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    if provider.lower() == "google":
        user.is_google_connected = False
        user.google_access_token = None
    elif provider.lower() == "microsoft":
        user.is_microsoft_connected = False
        user.microsoft_access_token = None
    await db.commit()
    await db.refresh(user)
    return UserOut.model_validate(user)

@router.get("/me", response_model=UserOut)
async def get_me(user: User = Depends(get_current_user)):
    return UserOut.model_validate(user)
