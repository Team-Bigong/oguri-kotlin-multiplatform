export type TravelInviteLanguage = "en" | "ko" | "ja" | "zh"

type InviteCopy = {
  title: string
  description: string
  openLabel: string
  installInstructions: string
  invalidDescription: string
}

const copy: Record<TravelInviteLanguage, InviteCopy> = {
  en: {
    title: "Trip invitation",
    description: "Review the invitation in Oguri and plan your trip together. Sign in to join.",
    openLabel: "Open in Oguri",
    installInstructions: "If you do not have the app, install it and open the same invitation link again.",
    invalidDescription: "This invitation link is invalid. Ask your friend for the original link.",
  },
  ko: {
    title: "여행 초대",
    description: "오구리 앱에서 초대를 확인하고 함께 여행을 계획할 수 있어요. 참여하려면 로그인해 주세요.",
    openLabel: "오구리에서 열기",
    installInstructions: "앱이 없다면 설치한 뒤 받은 초대 링크를 다시 열어 주세요.",
    invalidDescription: "올바르지 않은 초대 링크예요. 친구에게 원래 초대 링크를 다시 받아 주세요.",
  },
  ja: {
    title: "旅行への招待",
    description: "Oguriアプリで招待を確認し、一緒に旅行を計画できます。参加するにはログインしてください。",
    openLabel: "Oguriで開く",
    installInstructions: "アプリがない場合は、インストール後に同じ招待リンクをもう一度開いてください。",
    invalidDescription: "この招待リンクは正しくありません。友達に元のリンクをもう一度送ってもらってください。",
  },
  zh: {
    title: "旅行邀请",
    description: "在 Oguri 应用中查看邀请并一起规划旅行。请登录后加入。",
    openLabel: "在 Oguri 中打开",
    installInstructions: "如果尚未安装应用，请安装后重新打开同一个邀请链接。",
    invalidDescription: "此邀请链接无效。请让朋友重新发送原始链接。",
  },
}

const INVITE_TOKEN_LENGTH = 43

export function travelInviteModel(hash: string, languages: readonly string[]) {
  const supported: readonly string[] = ["en", "ko", "ja", "zh"]
  const language = languages
    .map((value) => value.toLowerCase().split(/[-_]/)[0])
    .find((value) => supported.includes(value)) as TravelInviteLanguage | undefined
  const locale = language ?? "en"
  const token = hash.startsWith("#") ? hash.slice(1) : ""
  const valid = token.length === INVITE_TOKEN_LENGTH && !/[^A-Za-z0-9_-]/.test(token)
  const messages = copy[locale]
  return {
    language: locale,
    title: messages.title,
    description: valid ? messages.description : messages.invalidDescription,
    openLabel: messages.openLabel,
    installInstructions: messages.installInstructions,
    appUrl: valid ? `oguri://open/trip-invite#${token}` : null,
  }
}
