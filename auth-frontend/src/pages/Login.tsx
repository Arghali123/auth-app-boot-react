import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { motion } from "framer-motion";
import { Lock, Mail } from "lucide-react";
import OAuth2Buttons from "@/components/OAuth2Buttons";

function Login() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 py-10 text-foreground">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8 }}
        className="w-full max-w-md"
      >
        <Card className="rounded-2xl border-border bg-card/70 p-6 shadow-2xl backdrop-blur-xl">
          <CardContent>
            <motion.h1
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.2 }}
              className="text-center text-4xl font-bold"
            >
              Welcome Back
            </motion.h1>

            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.4 }}
              className="mt-2 text-center text-muted-foreground"
            >
              Login to access your authentication app
            </motion.p>

            <form className="mt-8 space-y-6">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <div className="relative">
                  <Mail className="absolute top-1/2 left-3 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="email"
                    type="email"
                    placeholder="you@example.com"
                    className="pl-10"
                    name="email"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <div className="relative">
                  <Lock className="absolute top-1/2 left-3 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    id="password"
                    type="password"
                    placeholder="********"
                    className="pl-10"
                    name="password"
                  />
                </div>
              </div>

              <Button
                type="submit"
                className="w-full cursor-pointer rounded-2xl text-lg"
              >
                Login
              </Button>

              <div className="my-4 flex items-center gap-4">
                <div className="h-[1px] flex-1 bg-border"></div>
                <span className="text-sm text-muted-foreground">OR</span>
                <div className="h-[1px] flex-1 bg-border"></div>
              </div>

              <OAuth2Buttons />
            </form>
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
}

export default Login;
