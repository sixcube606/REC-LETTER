import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import {
    loginState,
    studioNameState,
    studioState,
} from '../util/counter-slice';
import { useEffect } from 'react';

export default function Mainpage() {
    /** 리덕스 설정 */
    const isLogin = useSelector((state: any) => state.loginFlag.isLogin);
    const dispatch = useDispatch();
    const navigator = useNavigate();

    useEffect(() => {
        dispatch(studioState(''));
        dispatch(studioNameState(''));
    }, []);

    useEffect(() => {
        const loginValue = localStorage.getItem('is-login');
        if (loginValue === 'true') {
            dispatch(loginState(true));
        }
        if (isLogin && loginValue) {
            navigator(`/studiolist`);
        }
    }, [isLogin]);
    /** 리덕스 설정 */

    return (
        <div className="relative base-height color-bg-subbold z-10">
            <div className="color-bg-black h-20 absolute top-0 w-full z-30 flex justify-around items-center text-white">
                <p>monofilm</p>
                <p>monofilm</p>
                <p>monofilm</p>
            </div>
            <img
                className="base-height absolute w-48 left-20 z-20 overflow-y-auto"
                src="/src/assets/images/leftframe.png"
                alt=""
            />
            <section className="section-center">
                <div className="flex flex-col justify-center items-center">
                    <p className="text-3xl mb-4">
                        영상으로 전하는 우리의 마음, 레크레터
                    </p>
                    <img
                        className="w-96 object-cover"
                        src="/src/assets/images/logo.png"
                        alt=""
                    />
                    <Link
                        to="./login"
                        className="w-60 bg-white cursor-pointer my-6 p-4 flex justify-center items-center border border-black rounded-md"
                    >
                        <span className="material-symbols-outlined">login</span>
                        <p className="text-3xl mx-4">시작하기</p>
                    </Link>
                </div>
            </section>
            <div className="color-bg-black h-20 absolute bottom-0 w-full z-30 flex justify-around items-center text-white"></div>
        </div>
    );
}
