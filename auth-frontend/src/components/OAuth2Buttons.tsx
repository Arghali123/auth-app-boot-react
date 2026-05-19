import { Button } from "./ui/button";
import { Globe, Shield } from "lucide-react";

function OAuth2Buttons() {


  return (
    <div className="space-y-3">
      <Button
        asChild
        variant="outline"
        className="w-full items-center gap-3 rounded-2xl"
      >
        <a href="#">
          <Globe />
          Continue with Google
        </a>
      </Button>

      <Button
        asChild
        variant="outline"
        className="w-full items-center gap-3 rounded-2xl"
      >
        <a href="/">
          <Shield />
          Continue with Github
        </a>
      </Button>
    </div>
  );
}

export default OAuth2Buttons;