import os
import boto3
from fastapi import APIRouter, BackgroundTasks

from videobackend.letter.dto.Req import MakeLetterReq
from videobackend.letter.service.LetterVideoService import create_letter

# 환경변수
AWS_ACCESS_KEY = os.environ.get('AWS_ACCESS')
AWS_SECRET_ACCESS_KEY = os.environ.get('AWS_SECRET')
REGION = os.environ.get('AWS_REGION')
BUCKET = os.environ.get('AWS_BUCKET')
SPRING_SERVER_URL = os.environ.get('SPRING_SERVER_URL') \
 \
# S3 클라이언트 설정
s3_client = boto3.client('s3', region_name=REGION,
                         aws_access_key_id=AWS_ACCESS_KEY,
                         aws_secret_access_key=AWS_SECRET_ACCESS_KEY)

# router
letter_video_router = APIRouter()


@letter_video_router.post("/letter")
async def make_letter(make_letter_dto: MakeLetterReq) -> None:
    print("make letter")
    print(make_letter_dto)
    BackgroundTasks.add_task(create_letter, make_letter_dto, BUCKET, s3_client)
    # create_letter(make_letter_dto, BUCKET, s3_client)
